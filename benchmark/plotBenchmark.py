import seaborn as sns
import matplotlib.pyplot as plt
import pandas as pd 


#load dataframe
path_to_csv = "benchmark/precision_recall.csv"
df = pd.read_csv(path_to_csv, sep = ';', index_col=False)
df = df.melt(id_vars =['LEFT','RIGHT','ALGORITHM'], 
            value_vars =["PRECISION","RECALL","FSCORE","TIME"],
            var_name ='metric')


#plot precision/recall/F-score
df_pr = df[df['metric'].isin(['PRECISION','RECALL'])]
sns.violinplot(data=df_pr, x='ALGORITHM', y='value', hue="metric", palette="pastel")
sns.stripplot(data=df_pr, x='ALGORITHM', y='value', hue="metric", dodge=True)
plt.figure()

#plot F-score
df_fscore = df[df['metric'].isin(['FSCORE'])]
sns.violinplot(data=df_fscore, x='ALGORITHM', y='value', palette="pastel")
sns.stripplot(data=df_fscore, x='ALGORITHM', y='value')
plt.figure()

#plot time
df_time = df[df['metric'].isin(['TIME'])]
sns.violinplot(data=df_time, x='ALGORITHM', y='value', palette="pastel")
sns.stripplot(data=df_time, x='ALGORITHM', y='value')
plt.show()